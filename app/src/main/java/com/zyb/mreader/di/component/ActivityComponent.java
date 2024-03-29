package com.zyb.mreader.di.component;



import com.zyb.base.di.component.AppComponent;
import com.zyb.base.di.scope.ActivityScope;
import com.zyb.mreader.di.module.ActivityModule;
import com.zyb.mreader.di.module.ApiModule;
import com.zyb.mreader.module.addBook.AddBookActivity;
import com.zyb.mreader.module.webdav.WebdavActivity;
import com.zyb.mreader.module.webdav.bookSelect.BookSelectActivity;
import com.zyb.mreader.module.webdav.login.LoginActivity;
import com.zyb.mreader.module.main.MainActivity;

import dagger.Component;

/**
 */
@ActivityScope
@Component(modules = {ActivityModule.class, ApiModule.class}, dependencies = AppComponent.class)
public interface ActivityComponent {

  void inject(AddBookActivity activity);
  void inject(MainActivity activity);
  void inject(WebdavActivity activity);
  void inject(LoginActivity activity);
  void inject(BookSelectActivity activity);
}
