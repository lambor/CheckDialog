# CheckDialog

<p align="center">
	<img src="https://github.com/lambor/CheckDialog/blob/master/art/device-2016-06-19-022019.gif?raw=true"/>
</p>


### Usage

##### Show a CheckDialog

```java
CheckDialog dialog = new CheckDialog(context);
dialog.show();
```

##### Dismiss the CheckDialog with result animation

Success
```java
dialog.OnOk();
```

Fail
```java
dialog.OnError();
```