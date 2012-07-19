package com.archermind.note.Events;


public interface IEventHandler {
	boolean onEvent(Object sender, EventArgs e);
}
