package com.zyb.mreader.di.component;



import com.zyb.base.di.component.AppComponent;
import com.zyb.base.di.scope.FragmentScope;
import com.zyb.mreader.di.module.ApiModule;
import com.zyb.mreader.di.module.FragmentModule;
import com.zyb.mreader.module.addBook.file.BookFilesFragment;
import com.zyb.mreader.module.addBook.path.BookPathFragment;

import dagger.Component;

/**
 */
@FragmentScope
@Component(modules = {FragmentModule.class, ApiModule.class}, dependencies = AppComponent.class)
public interface FragmentComponent {

    void inject(BookFilesFragment fragment);
    void inject(BookPathFragment fragment);

}
