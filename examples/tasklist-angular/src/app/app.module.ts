import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {registerLocaleData} from '@angular/common';
import {FormsModule} from '@angular/forms';
import localeFr from '@angular/common/locales/fr';
import localeDe from '@angular/common/locales/de';
import localeEn from '@angular/common/locales/en';

import {ApiModule} from 'tasklist/api.module';
import {AppComponent} from 'app/app.component';
import {ExternalUrlDirective} from 'app/components/external-url.directive';
import {PageNotFoundComponent} from 'app/components/page-not-found/page-not-found.component';
import {AppRoutingModule} from './app-routing.module';
import {FooterComponent} from './footer/footer.component';
import {HeaderComponent} from './header/header.component';
import {SearchComponent} from './search/search.component';
import {UserModule} from 'app/user/user.module';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';
import {ProcessModule} from 'app/process/process.module';
import {TaskModule} from 'app/task/task.module';


registerLocaleData(localeFr, 'fr');
registerLocaleData(localeDe, 'de');
registerLocaleData(localeEn, 'en');

export function storeLogger(reducer) {
  return (state, action: any): any => {
    const result = reducer(state, action);
    console.groupCollapsed(action.type);
    console.log('prev state', state);
    console.log('action', action);
    console.log('next state', result);
    console.groupEnd();

    return result;
  };
}

@NgModule({
  declarations: [
    AppComponent,
    ExternalUrlDirective,
    PageNotFoundComponent,
    FooterComponent,
    HeaderComponent,
    SearchComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    AppRoutingModule,
    NgbModule,
    ApiModule,
    UserModule,
    ProcessModule,
    TaskModule,
    StoreModule.forRoot({}, {
      metaReducers: [storeLogger]
    }),
    EffectsModule.forRoot([])
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
