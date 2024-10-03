
Java enums will be automatically converted to and from strings.

```
public enum UserType {
	
	DESTROYER_OF_WORLDS;
	
}

public class User {

	private UserType type;
	
	public UserType getType() {
		return type;
	}
	
	public void setType(final UserType type) {
		this.type = type;
	}
}

	
public class UserDto {

	private String type;

	public String getType() {
		return type;
	}
	
	public void setType(final String type) {
		this.type = type;
	}
}
```

You can convert any object from any of the classes above to an object of the other class with the following syntax:

```
...
import static com._8x8.converter.Converted.convert;
...
User user = new User();
user.setType(UserType.DESTROYER_OF_WORLDS);
...
UserDto userDto = convert(user).to(UserDto::new);
```

The userDto object will have the ``"DESTROYER_OF_WORLDS"`` String in the type member.

You can also do it the other way around:

```
...
import static com._8x8.converter.Converted.convert;
...
UserDto userDto = new UserDto();
user.setType("DESTROYER_OF_WORLDS");
...
User user = convert(userDto).to(User::new);
```

The user object will have the ``UserType.DESTROYER_OF_WORLDS`` enum value in the type member.
