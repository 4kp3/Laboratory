package com.lovely.bear.laboratory;

import java.util.ArrayList;
import java.util.List;

public class TestJava {
    public static void main(String[] args) {
        List<Integer> listChild = new ArrayList<>(2);
        List<? extends Number> listParent;
        listParent = listChild;
        // 允许取
        // 上界限定后所有的元素都是上界类型或者子类
        Number n1 = listParent.get(0);
        // 禁止存，因为不知道子类型是什么
        // 比如这里子列表元素类型是 Integer，如果通过父类存入 Number 的另一个子类 Float
        // 当子列表运行时取出这个 Float 后进行强制转换为 Integer 就会发生异常
        // listParent.add(new Object());// ❌

        List<Number> listChild2 = new ArrayList<>(2);
        List<? super Integer> listParent2;
        listParent2 = listChild2;
        // 下界，允许存
        // Number 可以看作是 Number? super于 Integer 的一个子集
        // 对父类的存操作是安全的，因为存入的元素一定是 Integer 和其子类
        // Integer 和其子类也一定是 Number 的子类，所以可以安全存入子类 Number 列表中
        // 子类型 child 自己后续的操作一定安全
        listParent2.add(2);
        // 只能取出 Object 类型元素
        Object e1=listParent2.get(0);
        // 因为满足 ? super Integer 的类型有很多，Object super Integer 也满足
        // 所以不能假定一定是 Number
        // Number e2=listParent2.get(0); // ❌

        //listChild.removeIf()
    }
}
