package example.list;

import java.util.ArrayList;

public class TestModel {
    public int position;
    public String title;

    public ArrayList<TestModel> list = new ArrayList<>();
    public int pid;

    public TestModel(int position, String title) {
        this.position = position;
        this.title = title;
    }


}
