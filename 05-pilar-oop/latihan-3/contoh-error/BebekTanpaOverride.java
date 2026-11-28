// Dikompilasi dengan sengaja: konflik default method tidak diselesaikan
interface Terbang   { default void bergerak() { System.out.println("terbang"); } }
interface Berenang  { default void bergerak() { System.out.println("berenang"); } }

class Bebek implements Terbang, Berenang { }
