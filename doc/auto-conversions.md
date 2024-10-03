
The generic converter will auto convert objects through methods that respect the following criteria:

- they are static
- they exist in the destination class
- they have the destination type assignable from the return type  
- they have only one parameter
- the parameter type is assignable from the source type

You can find a predicate defining this functionality in ``com._8x8.converter.tools.MethodPredicates.isConverterMethod()``

Now given these two classes:

```

public class User {

	private LocalDateTime deathDateTime;
	
	public LocalDateTime getDeathDateTime() {
		return deathDateTime;
	}
	
	public void setDeathDateTime(final LocalDateTime deathDateTime) {
		this.deathDateTime = deathDateTime;
	}
}

	
public class UserDto {

	private String deathDateTime;

	public String getDeathDateTime() {
		return deathDateTime;
	}
	
	public void setDeathDateTime(final String deathDateTime) {
		this.deathDateTime = deathDateTime;
	}
}
```

You can convert any object from any of the classes above to an object of the other class with the following syntax:

```
...
import static com._8x8.converter.Converted.convert;
...
UserDto userDto = new UserDto();
user.setDeathDateTime("2016-11-28T00:10:23.265");
...
User user = convert(userDto).to(User::new);
```

The user object will have the deathDateTime member as a LocalDateTime object constructed through the:
``LocalDateTime.parse(CharSequence)`` method.

You can also do it the other way around:

```
...
import static com._8x8.converter.Converted.convert;
...
User user = new User();
user.setDeathDateTime(LocalDateTime.now());
...
UserDto userDto = convert(user).to(UserDto::new);
```

The userDto object will have the date/time string in the deathDateTime member.
