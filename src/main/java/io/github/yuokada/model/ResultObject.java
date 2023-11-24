package io.github.yuokada.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultObject {

    @JsonProperty
    int result;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
