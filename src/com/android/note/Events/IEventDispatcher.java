package com.android.note.Events;


public interface IEventDispatcher {
	boolean add(IEventHandler handler);
	boolean remove(IEventHandler handler);
}

