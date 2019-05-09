package com.zyb.reader.di.component;


import com.zyb.base.di.component.AppComponent;
import com.zyb.base.di.scope.FragmentScope;
import com.zyb.reader.di.module.ApiModule;
import com.zyb.reader.di.module.FragmentModule;

import dagger.Component;

/**
 */
@FragmentScope
@Component(modules = {FragmentModule.class, ApiModule.class}, dependencies = AppComponent.class)
public interface FragmentComponent {


}
