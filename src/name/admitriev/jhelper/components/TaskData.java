package name.admitriev.jhelper.components;

class RawTest {
    public String input;
    public String output;
}

class IoConfig {
    public String type;
}

class Batch {
    public String id;
    public int size;
}

class Languages {

}

// Task data returned by CompetitiveCompanion
class CcTaskData {
    public String name;
    public String group;
    public String url;
    public Boolean interactive;
    public int memoryLimit;
    public int timeLimit;
    public RawTest[] tests;
    public String testType;
    public IoConfig input;
    public IoConfig output;
    public transient Languages languages;
    public transient Batch batch;

    public String taskClass() {
        return name.replaceAll("[^a-zA-Z0-9]", "");
    }
}
