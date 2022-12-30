package com.rr.personal.data;

import android.os.*;
import java.io.*;
import java.util.*;

public class RecursiveFileObserver extends FileObserver
{

    private final Map<String, FileObserver> mObservers = new HashMap<>();
    private final String mPath;
    private final int mMask;
    private final EventListener mListener;

    public interface EventListener {
        void onEvent(int event, File file);
    }

    public RecursiveFileObserver(String path, EventListener listener) {
        this(path, ALL_EVENTS, listener);
    }

    public RecursiveFileObserver(String path, int mask, EventListener listener) {
        super(path, mask);
        mPath = path;
        mMask = mask | FileObserver.CREATE | FileObserver.DELETE_SELF;
        mListener = listener;
    }

    private void startWatching(String path) {
        synchronized (mObservers) {
            FileObserver observer = mObservers.remove(path);
            if (observer != null) {
                observer.stopWatching();
            }
            observer = new SingleFileObserver(path, mMask);
            observer.startWatching();
            mObservers.put(path, observer);
        }
    }

    @Override
    public void startWatching() {
        Stack<String> stack = new Stack<>();
        stack.push(mPath);

        while (!stack.empty()) {
            String parent = stack.pop();
            startWatching(parent);

            File path = new File(parent);
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (watch(file)) {
                        stack.push(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    private boolean watch(File file) {
        return file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..");
    }

    private void stopWatching(String path) {
        synchronized (mObservers) {
            FileObserver observer = mObservers.remove(path);
            if (observer != null) {
                observer.stopWatching();
            }
        }
    }

    @Override
    public void stopWatching() {
        synchronized (mObservers) {
            for (FileObserver observer : mObservers.values()) {
                observer.stopWatching();
            }
            mObservers.clear();
        }
    }

    @Override
    public void onEvent(int event, String path) {
        File file;
        if (path == null) {
            file = new File(mPath);
        } else {
            file = new File(mPath, path);
        }
        notify(event, file);
    }

    private void notify(int event, File file) {
        if (mListener != null) {
            mListener.onEvent(event & FileObserver.ALL_EVENTS, file);
        }
    }

    private class SingleFileObserver extends FileObserver {
        private final String filePath;

        public SingleFileObserver(String path, int mask) {
            super(path, mask);
            filePath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            File file;
            if (path == null) {
                file = new File(filePath);
            } else {
                file = new File(filePath, path);
            }

            switch (event & FileObserver.ALL_EVENTS) {
				case DELETE_SELF:
					RecursiveFileObserver.this.stopWatching(filePath);
					break;
				case CREATE:
					if (watch(file)) {
						RecursiveFileObserver.this.startWatching(file.getAbsolutePath());
					}
					break;
            }

            RecursiveFileObserver.this.notify(event, file);
        }
    }
}
