package com.medisons.dbm;

import java.util.Objects;

public class BackgroundData {

    private final Integer age;
    private final Integer weight;
    private final Integer height;
    private final String sex;

    public BackgroundData(Integer age, Integer weight, Integer height, String sex) {
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public Integer getWeight() {
        return weight;
    }

    public Integer getHeight() {
        return height;
    }

    public String getSex() {
        return sex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BackgroundData that = (BackgroundData) o;
        return Objects.equals(age, that.age) &&
                Objects.equals(weight, that.weight) &&
                Objects.equals(height, that.height) &&
                Objects.equals(sex, that.sex);
    }
}
