package com.zyb.reader.di.component;


import com.zyb.base.di.component.AppComponent;
import com.zyb.base.di.scope.ActivityScope;
import com.zyb.reader.di.module.ActivityModule;
import com.zyb.reader.di.module.ApiModule;
import com.zyb.reader.read.ReadActivity;

import dagger.Component;

/**
 */
@ActivityScope
@Component(modules = {ActivityModule.class, ApiModule.class}, dependencies = AppComponent.class)
public interface ActivityComponent {

  void inject(ReadActivity activity);
}
