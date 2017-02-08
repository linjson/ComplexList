package example.fixheader;

import com.github.linjson.DiffCallBackEx;

import java.util.List;

/**
 * Created by ljs on 2016/12/1.
 */

public class Diff extends DiffCallBackEx {

    private final School news;
    private final School olds;

    public Diff(GroupRecyclerAdapter adapter, School olds, School news) {
        super(adapter);
        this.olds = olds;
        this.news = news;

    }

    @Override
    protected Object getDataChangePayload(int oldItemPosition, int newItemPosition) {

//        int[] newPos = getGroupSonPosition(news.clazz(), newItemPosition);
//
//        Bundle bundle = new Bundle();
//        if (newPos[1] == -1) {
//            bundle.putString("test", news.clazz().get(newPos[0]).name());
//        } else {
//            bundle.putString("test", news.clazz().get(newPos[0]).student().get(newPos[1]).name());
//        }
//        return bundle;
        return null;
    }

    private int getSize(School test) {
        int size = test.clazz().size();
        int t = size;

        for (int i = 0; i < size; i++) {
            t += test.clazz().get(i).student().size();
        }

        return t;
    }

    @Override
    public int getOldDataSize() {
        return getSize(olds);
    }


    @Override
    public int getNewDataSize() {
        return getSize(news);
    }

    private int[] getGroupSonPosition(List<Clazz> list, int pos) {

        int groupSize = list.size();
        int[] index = new int[2];
        int p = pos;
        for (int i = 0; i < groupSize; i++) {
            int temp = p - list.get(i).student().size() - 1;
            if (temp < 0) {
                index[0] = i;
                index[1] = p - 1;
                return index;
            } else {
                p = temp;
            }
        }
        return index;
    }

    @Override
    public boolean areDataTheSame(int oldItemPosition, int newItemPosition) {

        return oldItemPosition==newItemPosition;
    }

    @Override
    public boolean areDataContentsTheSame(int oldItemPosition, int newItemPosition) {

        int[] oldPos = getGroupSonPosition(olds.clazz(), oldItemPosition);
        int[] newPos = getGroupSonPosition(news.clazz(), newItemPosition);
//            String o = "", n = "";
        Object o = null, n = null;
        if (oldPos[1] == -1) {
            o = olds.clazz().get(oldPos[0]);
        } else {
            o = olds.clazz().get(oldPos[0]).student().get(oldPos[1]);
        }
        if (newPos[1] == -1) {
            n = news.clazz().get(newPos[0]);
        } else {
            n = news.clazz().get(newPos[0]).student().get(newPos[1]);
        }

        return o.equals(n);
    }


}