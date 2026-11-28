// Dikompilasi dengan sengaja: @Override pada method static
class Induk {
    static void info() { }
}

class Anak extends Induk {
    @Override
    static void info() { }
}
