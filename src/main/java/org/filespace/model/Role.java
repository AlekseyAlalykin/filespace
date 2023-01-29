package org.filespace.model;

public enum Role {
    CREATOR(3),
    ADMINISTRATOR(2),
    CONTRIBUTOR(1),
    SPECTATOR(0);


    private final int weight;
    Role(int weight){
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
