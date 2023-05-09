package org.pytorch.demo.objectdetection;

import java.util.ArrayList;

class Labels {
    String name = "";
    double x = 0;
    double y = 0;
}

public class Priority {
    private static int base_point_x = 0;
    private static int base_point_y = 0;

    private static double len(double x, double y) {
        return Math.sqrt(Math.pow((x - base_point_x), 2) + Math.pow((y - base_point_y), 2));
    }

    private static double mid(int x_max, int x_min) {
        return Double.valueOf(x_max + x_min) / 2;
    }

    static ArrayList<Labels> input(ArrayList<Result> result) {
        ArrayList<Labels> list = new ArrayList<Labels>();
        for (int i = 0; i < result.size(); i++) {
            Labels r = new Labels();
            r.name = PrePostProcessor.mClasses[result.get(i).classIndex];
            r.x = mid(result.get(i).rect.left, result.get(i).rect.right);
            r.y =Double.valueOf(result.get(i).rect.bottom);
            list.add(r);
        }
        return list;
    }

    private static String directions(double mid, int viewWidth) {
        String direction = "";
        double left = viewWidth / 4;
        double right = viewWidth * 3 / 4;

        if (mid < left) {
            direction = "좌측";
        }
        else if (mid > right) {
            direction = "우측";
        }
        else {
            direction = "정면";
        }

        return direction;
    }

    // 이 부분에서 각각의 요소 값들의 이름을 지정
    static ArrayList<String> priority(ArrayList<Labels> list, int deviceWidth) {
        int firstIndex = 0;
        int secondIndex = 0;
        int count = list.size();

        for (int i = 1; i < count; i++) {
            // 최단거리 구해서 작성 (x,y)좌표 이용, 기존 위치 = ?
            double distance = len(list.get(i).x, list.get(i).y);
            if (distance < len(list.get(firstIndex).x, list.get(firstIndex).y)) {
                secondIndex = firstIndex;
                firstIndex = i;
            }
            else if (distance < len(list.get(secondIndex).x, list.get(secondIndex).y)) {
                secondIndex = i;
            }
        }

        // 마지막에 첫 번째 물체의 방향 및 이름, 두 번째 물체의 방향 및 이름, 마지막으로 2개를 제외한 나머지 개수
        ArrayList<String> results = new ArrayList<>();
        results.add(directions(list.get(firstIndex).x, deviceWidth));
        results.add(list.get(firstIndex).name);
        results.add(directions(list.get(secondIndex).x, deviceWidth));
        results.add(list.get(secondIndex).name);
        results.add(Integer.toString(count - 2));

        return results;
    }
}
