package com.ljs.complexlist;

import java.util.ArrayList;

public class TestModel implements Cloneable {
    public int position;
    public String title;

    public ArrayList<TestModel> list = new ArrayList<>();
    public int pid;

    public TestModel(int position, String title) {
        this.position = position;
        this.title = title;
    }

    public TestModel clone() {
        TestModel t = new TestModel(this.position, this.title);
        t.list = new ArrayList<>(list);
        t.pid = pid;

        return t;
    }


}
