
Given these two classes:

```
public class User {

	private String name;
	
	public String getName() {
		return name;
	}
	
	public void setName(final String name) {
		this.name = name;
	}
}

	
public class UserDto {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
	
}
```

You can convert any object from any of the classes above to an object of the other class with the following syntax:

```
...
import static com._8x8.converter.Converted.convert;
...
User user = new User();
user.setName("Foo");
...
UserDto userDto = convert(user).to(UserDto::new);
```
The above example will convert a User object to a UserDto object so that when we call the getName() method on the destination object the name will be the same as in the source object.

The converter will look through the members in the destination objects' type and look for the similar members in the destination object, in this case the name. 