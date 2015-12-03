public class Pair<E, F> {

	// O primeiro elemento do par
	private final E first;
	// O segundo elemento do par
	private final F second;

	public Pair(E first, F second) {
		this.first = first;
		this.second = second;
	}

	/*
	 * Devolve o primeiro elemento do par
	 * @return o primeiro elemento do par
	 */
	public E getFirst() {
		return first;
	}

	/*
	 * Devolve o segundo elemento do par
	 * @return o segundo elemento do par
	 */
	public F getSecond() {
		return second;
	}
	
	@Override
	public String toString(){
		return getFirst().toString()+" "+getSecond().toString();
	}
}