
package org.infinispan.test.hibernate.cache.commons.functional.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;

/**
 * @author Galder Zamarreño
 */
@NamedQueries({@NamedQuery(name=Age.QUERY, query = "SELECT a FROM Age a")})
@Entity
public class Age {

   public static final String QUERY = "Age.findAll";

	@Id
	@GeneratedValue
	private Integer id;
	private Integer age;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

   public Integer getAge() {
      return age;
   }

   public void setAge(Integer age) {
      this.age = age;
   }
}
