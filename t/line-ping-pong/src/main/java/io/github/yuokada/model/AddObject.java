package io.github.yuokada.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddObject {

    @JsonProperty("right")
    int right;
    @JsonProperty("left")
    int left;

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    @Override
    public String toString() {
        return "AddObject{" +
            "right=" + right +
            ", left=" + left +
            '}';
    }
}
