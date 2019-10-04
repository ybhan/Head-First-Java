import java.util.ArrayList;

// This is the same example as the illustration in Page 140.
class DotComBustTestDrive {  // Need to set as Public DotComBust.dotComsList and DotComBust.startPlaying
    DotComBust game = new DotComBust();

    public static void main(String[] args) {
        DotComBustTestDrive test = new DotComBustTestDrive();
        test.setTestGame();
        test.game.startPlaying();
    }

    void setTestGame() {
        ArrayList<DotCom> dotComTest = new ArrayList<DotCom>();

        DotCom dotCom1 = new DotCom();
        DotCom dotCom2 = new DotCom();
        DotCom dotCom3 = new DotCom();
        dotCom1.setName("Pets.com");
        dotCom2.setName("Go2.com");
        dotCom3.setName("AskMe.com");

        ArrayList<String> loc1 = new ArrayList();
        ArrayList<String> loc2 = new ArrayList();
        ArrayList<String> loc3 = new ArrayList();
        loc1.add("d2");
        loc1.add("d3");
        loc1.add("d4");
        loc2.add("b0");
        loc2.add("c0");
        loc2.add("d0");
        loc3.add("g3");
        loc3.add("g4");
        loc3.add("g5");
        dotCom1.setLocationCells(loc1);
        dotCom2.setLocationCells(loc2);
        dotCom3.setLocationCells(loc3);

        dotComTest.add(dotCom1);
        dotComTest.add(dotCom2);
        dotComTest.add(dotCom3);

        game.dotComsList = dotComTest;
    }
}
