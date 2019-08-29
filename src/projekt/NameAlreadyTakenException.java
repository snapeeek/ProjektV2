package projekt;

public class NameAlreadyTakenException extends Exception
{
    public NameAlreadyTakenException() {}

    public String GetWarning()
    {
        return "Name is already taken by someone else!";
    }
}
