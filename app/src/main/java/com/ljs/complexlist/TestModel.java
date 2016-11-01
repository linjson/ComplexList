package com.ljs.complexlist;

public class TestModel {
    public boolean group;
    public int position;
    public String title;
    public int pid;
    public TestModel(int position, String title, boolean group) {
        this.position = position;
        this.title = title;
        this.group = group;
    }

    public TestModel(int position, String title) {
        this(position, title, false);

    }
}
