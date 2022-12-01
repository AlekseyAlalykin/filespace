package org.filespace.services;

import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

@Service
public class ValidationService {

    public <T> boolean validate(T entity){
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        return validator.validate(entity).size() == 0 ? true : false;
    }


    public  <T> String getConstrainsViolations(T entity){
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Set<ConstraintViolation<T>> constraintsViolations = validator.validate(entity);
        StringBuilder builder = new StringBuilder();

        for(ConstraintViolation<T> violation: constraintsViolations){
            //Property: {propertyName} Value: {value} Message: {message}
            builder.append("Property: ");
            builder.append(violation.getPropertyPath());
            builder.append(" Value: ");
            builder.append(violation.getInvalidValue());
            builder.append(" Message: ");
            builder.append(violation.getMessage());
        }

        return builder.toString();
    }
}
