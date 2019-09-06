package projekt;

/**
 * klasa rozszerzajaca wyjatek, ktora rzucana jest gdy podane imie jest juz zajete
 */
public class NameAlreadyTakenException extends Exception
{
    /**
     * pusty konstruktor
     */
    public NameAlreadyTakenException() {}

    /**
     * funkcja zwracajaca komunikat o bledzie
     * @return komunikat
     */
    public String GetWarning()
    {
        return "Name is already taken by someone else!";
    }
}
