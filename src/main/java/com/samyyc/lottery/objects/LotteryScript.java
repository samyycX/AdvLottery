package com.samyyc.lottery.objects;

import com.samyyc.lottery.miscs.VariableKeyValue;
import com.samyyc.lottery.utils.ExtraUtils;

import java.util.*;

public class LotteryScript implements Iterable<LotteryScript> {

    private String name;
    private Stack<String> taskStack = new Stack<>();
    private LotteryScript sonStack;
    private LotteryScript fatherStack;
    private int sonStackDeep = 0;
    private int fatherStackDeep = 0;
    private int loopTime = 0;
    private boolean complete = false;

    public LotteryScript(LotteryScript fatherStack) {
        this.fatherStack = fatherStack;
    }

    public LotteryScript(LotteryScript fatherStack, String scriptName) {
        this.fatherStack = fatherStack;
        this.name = scriptName;
    }

    public void parseScript(List<String> list, Map<String, String> variable) {
        boolean startPush = false;
        String waitFor = null;
        if (name == null) {
            for (String text : list) {
                if (text.startsWith("::")) {
                    name = text.replace("::", "");
                    break;
                }
            }
        }
        for (String text : list) {
            if (startPush) {
                if (!text.equalsIgnoreCase("end::" + name)) {
                    if (text.startsWith("::")) {
                        startPush = false;
                        waitFor = text.replace("::", "");
                        if (sonStackDeep == 0) {
                            sonStack = new LotteryScript(this, text.replace("::", ""));
                            sonStack.parseScript(list, variable);
                        }
                        sonStackDeep++;
                    }
                    if (startPush) taskStack.push(text);
                } else {
                    startPush = false;
                }
            } else if (text.equalsIgnoreCase("::" + name)) {
                startPush = true;
            } else if (waitFor != null) {
                if (text.replace("end::", "").equals(waitFor)) {
                    startPush = true;
                }
            }
            if (text.startsWith("循环")) {
                String[] splited = text.split(" ");
                if (splited.length >= 3) {
                    String name = splited[1];
                    String times = splited[2];
                    Iterator<LotteryScript> iterator = iterator();
                    int time;
                    try {
                        time = Integer.parseInt(times);
                    } catch (NumberFormatException e) {
                        time = Integer.parseInt(ExtraUtils.processStatement(times, variable));
                    }
                    if (getName().equals(name)) {
                        loopTime = time;
                        break;
                    }
                    if (sonStack != null) {
                        if (sonStack.getName().equals(name)) {
                            sonStack.loopTime = time;
                        }
                    }
                }
            }

        }
        LotteryScript stack = this;
        while (hasFatherStack(stack)) {
            fatherStackDeep++;
            stack = stack.fatherStack;
        }
        taskStack.removeIf(text -> {
            if (text.startsWith("循环")) {
                String[] splited = text.split(" ");
                return splited.length >= 3;
            } else {
                return false;
            }
        });

    }


    public String getName() {
        return name;
    }

    public LotteryScript getSonStack() {
        return sonStack;
    }

    public boolean hasFatherStack(LotteryScript stack) {
        return stack.fatherStack != null;
    }

    public LotteryScript getFatherStack() {
        if (fatherStack != null) {
            return fatherStack;
        } else {
            return null;
        }

    }

    public int getFatherStackDeep() {
        return fatherStackDeep;
    }

    @Override
    public ListIterator<LotteryScript> iterator() {
        return new StackIterator(this);
    }

   public void setLoopTime(int time) {
        this.loopTime = time;
   }


    public List<String> processBlock(HashMap<String, String> variableMap) {
        if (variableMap == null) variableMap = new HashMap<>();


        final LinkedList<VariableKeyValue> timesList = new LinkedList<>();

        Iterator<LotteryScript> iterator = iterator();
        timesList.add(new VariableKeyValue(name, loopTime));
        while (iterator.hasNext()) {
            LotteryScript stack = iterator.next();
            if (stack.loopTime!=0) {
                timesList.add(new VariableKeyValue(stack.name, stack.loopTime));
            }
        }

        Collections.reverse(timesList);

        LinkedList<Stack<Integer>> stackList = new LinkedList<>();
        for (int i = 0; i < timesList.size(); i++) {
            Stack<Integer> stack = new Stack<>();
            stackList.add(stack);
        }
        List<Integer> valueChanged = new ArrayList<>();
        int first = 0;
        List<String> statementList = new ArrayList<>();
        while (stackList.getLast().size() != timesList.getLast().value()) {
            if (first != 0) {
                stackList.get(0).push(1);
            } else {
                first++;
            }
            for (int j = 0; j < stackList.size(); j++) {
                if (stackList.get(j).size() == timesList.get(j).value()) {
                    if (j + 1 != stackList.size()) {
                        stackList.get(j).clear();
                        stackList.get(j + 1).push(1);
                        valueChanged.add(j);
                        valueChanged.add(j+1);
                        break;
                    }
                } else {
                    valueChanged.clear();
                    valueChanged.add(0);
                }
            }
            Stack<String> taskStack1;
            String name = this.name;
            for (int depth : valueChanged) {
                taskStack1 = taskStack;
                String key = timesList.get(depth).key();
                Iterator<LotteryScript> iterator1 = iterator();
                while (iterator1.hasNext()) {
                    LotteryScript stack = iterator1.next();
                    if (stack.getName().equals(key)) {
                        taskStack1 = stack.taskStack;
                        name = stack.getName();
                    }
                }
                    for (String statement : taskStack1) {
                        HashMap<String, String> loopVariableMap = new HashMap<>();
                        for (int i = 0; i < timesList.size(); i++) {
                            loopVariableMap.put("循环数." + timesList.get(i).key(), String.valueOf(stackList.get(i).size()));
                        }
                        loopVariableMap.putAll(variableMap);
                        statementList.add(ExtraUtils.processStatement(statement, loopVariableMap));
                }
            }
            valueChanged.clear();
        }
        timesList.clear();
        stackList = null;
        variableMap = null;
        return statementList;
    }
}

class StackIterator implements ListIterator<LotteryScript> {

    private LotteryScript stack;
    private int depth;

    public StackIterator(LotteryScript stack) {
        this.stack = stack;
        depth = stack.getFatherStackDeep();
    }

    @Override
    public boolean hasNext() {
        return stack.getSonStack() != null;
    }

    public boolean hasPrevious() {
        return stack.hasFatherStack(stack);
    }

    @Override
    public LotteryScript next() {
        stack = stack.getSonStack();
        depth++;
        return stack;
    }

    public LotteryScript previous() {
        stack = stack.getFatherStack();
        depth--;
        return stack;
    }

    @Override
    public int nextIndex() {
        return depth;
    }

    @Override
    public int previousIndex() {
        return depth;
    }

    @Override
    public void remove() {
        // doesnt need
    }

    @Override
    public void set(LotteryScript stack) {
        // doesnt need
    }

    @Override
    public void add(LotteryScript stack) {
        // doesnt need
    }


}
