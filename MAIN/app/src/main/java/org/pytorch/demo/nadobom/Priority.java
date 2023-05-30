package org.pytorch.demo.nadobom;

import java.util.ArrayList;

class Labels {
    String name = "";
    double x = 0;
    double y = 0;
}

public class Priority {
//    private static int base_point_x = 0;
//    private static int base_point_y = 0;

    private static double len(double x, double y, int viewWidth, int viewHeight) {
        double base_point_x = viewWidth/2;
        double base_point_y = viewHeight;
//        Log.d("MyTag_len","base: "+base_point_x+", "+base_point_y);
        return Math.sqrt(Math.pow((x - base_point_x), 2) + Math.pow((y - base_point_y), 2));
    }

    private static double mid(int x_max, int x_min) {
        return Double.valueOf(x_max + x_min) / 2;
    }

    static ArrayList<Labels> input(ArrayList<Result> result, int viewHeight) {
        double limitMaxHeight = viewHeight / 5;
        ArrayList<Labels> list = new ArrayList<Labels>();

        for (int i = 0; i < result.size(); i++) {
            Labels r = new Labels();
            r.name = PrePostProcessor.mClasses[result.get(i).classIndex];
            r.x = mid(result.get(i).rect.left, result.get(i).rect.right);
            r.y =Double.valueOf(result.get(i).rect.bottom);
            if(r.y > limitMaxHeight) list.add(r);
        }

        return list;
    }
    private static String directions(double mid_x, double mid_y, int viewWidth, int viewHeight) {
        String direction = "";
        Double left_y = -viewHeight/(viewWidth*1.5/4 - viewWidth*1/5)*(mid_x-viewWidth*1/5);
        Double right_y = viewHeight/(viewWidth*2.5/4 - (viewWidth*4/5))*(mid_x-(viewWidth*4/5));

        if(left_y >= 0 && left_y <= viewHeight){
            if(mid_y < left_y){direction = "좌측";}
            else{direction = "정면";}
        }
        else if(right_y >=0 && right_y <= viewHeight) {
            if(mid_y < right_y){direction = "우측";}
            else{direction = "정면";}
        }
        else{direction = "정면";}
        return direction;
    }

    private static int getObstacleWeight(String obstacle){
        if(obstacle.equals("트럭")) return 0;
        if(obstacle.equals("킥보드") || obstacle.equals("자전거")) return 1;
        if(obstacle.equals("볼라드") || obstacle.equals("바리케이드")) return 2;
        return 3;
    }
    // 이 부분에서 각각의 요소 값들의 이름을 지정
    static ArrayList<String> priority(ArrayList<Labels> list, int viewWidth, int viewHeight) {
//        Log.d("MyTag_priority","size: "+viewWidth+", "+viewHeight);
        double limitMaxHeight = viewHeight / 5;
        ArrayList<String> results = new ArrayList<String>();

        if(list.size() < 1) return results;
        if(list.size() == 1){
            results.add(directions(list.get(0).x,list.get(0).y, viewWidth,viewHeight));
            results.add(list.get(0).name);
            if(list.get(0).y < limitMaxHeight) results.clear();
            return results;
        }

        int firstIndex = 0;
        int secondIndex = 1;
        int count = list.size();

//        Log.d("MyTag_priority()","name: "+list.get(0).name+" distance : "+len(list.get(0).x, list.get(0).y, viewWidth, viewHeight)+", x, y : " + list.get(0).x + ", " + list.get(0).y);
        for (int i = 1; i < count; i++) {
            // 최단거리 구해서 작성 (x,y)좌표 이용, 기존 위치 = ?
            double distance = len(list.get(i).x, list.get(i).y, viewWidth, viewHeight);
//            Log.d("MyTag_priority()","name: "+list.get(i).name+"distance : "+distance+", x, y : " + list.get(i).x + ", " + list.get(i).y);
            if (distance < len(list.get(firstIndex).x, list.get(firstIndex).y, viewWidth, viewHeight)) {
                secondIndex = firstIndex;
                firstIndex = i;
            }
            else if (distance < len(list.get(secondIndex).x, list.get(secondIndex).y, viewWidth, viewHeight)) {
                secondIndex = i;
            }
        }

        int minLength = 200; // issue 수정 필요
        int firstWeight = getObstacleWeight(list.get(firstIndex).name);
        int secondWeight = getObstacleWeight(list.get(secondIndex).name);

        if(len(list.get(secondIndex).x, list.get(secondIndex).y, viewWidth, viewHeight) - len(list.get(firstIndex).x, list.get(firstIndex).y, viewWidth, viewHeight) < minLength && firstWeight > secondWeight){
            int temp = firstIndex;
            firstIndex = secondIndex;
            secondIndex = temp;
        }

        // 마지막에 첫 번째 물체의 방향 및 이름, 두 번째 물체의 방향 및 이름, 마지막으로 2개를 제외한 나머지 개수
        results.add(directions(list.get(firstIndex).x,list.get(firstIndex).y, viewWidth,viewHeight));
        results.add(list.get(firstIndex).name);
        results.add(directions(list.get(secondIndex).x,list.get(secondIndex).y, viewWidth,viewHeight));
        results.add(list.get(secondIndex).name);
        results.add(Integer.toString(count - 2));

        return results;
    }
}
