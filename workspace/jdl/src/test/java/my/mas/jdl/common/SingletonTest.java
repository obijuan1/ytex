package my.mas.jdl.common;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SingletonTest {
	private static final Singleton singleton = Singleton.getIstance();

	@Test
	public void getIstance() {
		assertThat(singleton, notNullValue());
		assertThat(singleton, equalTo(Singleton.getIstance()));
		assertThat(singleton, sameInstance(Singleton.getIstance()));
		assertThat(Singleton.getIstance(), notNullValue());
		assertThat(Singleton.getIstance(), equalTo(Singleton.getIstance()));
		assertThat(Singleton.getIstance(), sameInstance(Singleton.getIstance()));
	}
}
