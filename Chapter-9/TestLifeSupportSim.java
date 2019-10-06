import java.util.*;
class V2Radiator {
    V2Radiator(ArrayList list) {
        for (int x=0; x<5; x++) {
            list.add(new SimUnit("V2Radiator"));
        }
    }
}

class V3Radiator extends V2Radiator {
    V3Radiator(ArrayList list) {
        super(list);
        for (int g=0; g<10; g++) {
            list.add(new SimUnit("V3Radiator"));
        }
    }
}

class RetentionBot {
    RetentionBot(ArrayList rlist) {
        rlist.add(new SimUnit("Retention"));
    }
}

public class TestLifeSupportSim {
    public static void main(String[] args) {
        ArrayList aList = new ArrayList();
        V2Radiator v2 = new V2Radiator(aList);
        V3Radiator v3 = new V3Radiator(aList);
        for (int z=0; z<20; z++) {
            RetentionBot ret = new RetentionBot(aList);
        }

        // Check the overall power
        int overallPower = 0;
        for (Object u: aList) {
            overallPower += ((SimUnit) u).powerUse();
        }
        System.out.println("Overall Power: " + overallPower);
    }
}

class SimUnit {
    String botType;
    SimUnit(String type) {
        botType = type;
        // Print a message when a SimUnit is created
        System.out.println("Create a SimUnit of " + botType);
    }
    int powerUse() {
        if ("Retention".equals(botType)) {
            return 2;
        } else {
            return 4;
        }
    }
}