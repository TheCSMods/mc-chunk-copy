package thecsdev.chunkcopy;

/**
 * Java does not have a {@link Tuple}
 * so I had to make one.
 */
public class Tuple<X, Y>
{
	public final X Item1;
	public final Y Item2;

	public Tuple(X item1, Y item2)
	{
		this.Item1 = item1;
		this.Item2 = item2;
	}
}