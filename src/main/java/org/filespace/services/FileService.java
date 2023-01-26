package org.filespace.services;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

@Service
public class FileService {

    private static String propertiesPath = "classpath:file-manager.properties";

    private static String storageDirectory;

    private static String temporaryDirectory;

    private static Long maxContentLength;

    public static Long getMaxContentLength() {
        return FileService.maxContentLength;
    }

    static {
        try {
            FileInputStream fileInputStream = new FileInputStream(
                    ResourceUtils.getFile(propertiesPath));
            Properties properties = new Properties();
            properties.load(fileInputStream);

            storageDirectory = properties.getProperty("storage-directory");
            temporaryDirectory = properties.getProperty("temporary-directory");
            maxContentLength = Long.parseLong(properties.getProperty("max-content-length"));

            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();

            storageDirectory = "C:/FileServiceRootDirectory";
            temporaryDirectory = "C:/temp";
            maxContentLength = 1024*1024*10L;
        }
    }

    public FileService(){
    }

    public String getRootDirectory() {
        return storageDirectory;
    }

    @Deprecated
    public void temporarySaveFile(MultipartFile multipartFile) throws Exception{
        File file = new File(temporaryDirectory + "/" + multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);
    }

    //Сохраняет файл на диск и возвращает md5 хэш сумму от файла
    public String temporarySaveFile(InputStream stream) throws Exception {

        String tempFilename = temporaryDirectory + "/" + stream.toString();

        File targetFile = new File(tempFilename);
        OutputStream outStream = new FileOutputStream(targetFile);

        byte[] buffer = new byte[8 * 1024];
        int bytesRead;

        while ((bytesRead = stream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        IOUtils.closeQuietly(stream);
        IOUtils.closeQuietly(outStream);

        return tempFilename;

        //String md5 = md5FileHash(tempFilename);

        //moveToStorageDirectory(md5,tempFilename);
    }

    //Находит хэш сумму от содержимого файла
    public String md5FileHash(String path) throws Exception {
        String hash;

        try (InputStream is = Files.newInputStream(Paths.get(path))) {
            hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
        }

        return hash;
    }

    //Перемещает временный файл в директорию хранилища в соответствии с его md5
    //Если по пути нет необходимых папок создает их
    //Если файл с данной хэш суммой уже есть тогда файл не перезаписывается и удаляется временный файл
    public void moveToStorageDirectory(String md5, String fullFilename) throws IOException{
        String filePath = getPathFromMD5(md5);

        File file = new File(filePath);
        if (file.exists()) {
            Files.delete(Path.of(fullFilename));
            return;
        }

        Files.createDirectories(Paths.get(filePath.substring(0,filePath.length() - 28)));
        Files.move(Path.of(fullFilename), Path.of(filePath), StandardCopyOption.ATOMIC_MOVE);
    }

    //Превращает md5 в путь
    private String getPathFromMD5(String md5){
        StringBuffer stringBuffer = new StringBuffer(md5);
        stringBuffer.insert(2,'/');
        stringBuffer.insert(5, '/');

        return storageDirectory + "/" + stringBuffer.toString();
    }

    public void deleteFile(String md5) throws IOException{
        String path = getPathFromMD5(md5);
        Files.deleteIfExists(Path.of(path));
    }

    public InputStreamResource getFile(String md5) throws Exception {
        InputStreamResource resource = new InputStreamResource(
                Files.newInputStream(
                        Path.of(getPathFromMD5(md5))));

        return resource;
    }

    public Long getFileSize(String md5) throws IOException{
        Path path = Paths.get(getPathFromMD5(md5));
        return Files.size(path);
    }
}
