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
import {TasklistComponent} from 'app/components/tasklist/tasklist.component';
import {TaskHelperService} from 'app/services/task.helper.service';
import {FieldNamePipe} from 'app/services/field-name.pipe';
import {FilterService} from 'app/services/filter.service';
import {SortableColumnComponent} from 'app/components/sorter/sortable-column.component';
import {ExternalUrlDirective} from 'app/components/external-url.directive';
import {PageNotFoundComponent} from 'app/components/page-not-found/page-not-found.component';
import {AppRoutingModule} from './app-routing.module';
import {ProcessHelperService} from './services/process.helper.service';
import {FooterComponent} from './footer/footer.component';
import {HeaderComponent} from './header/header.component';
import {SearchComponent} from './search/search.component';
import {UserModule} from 'app/user/user.module';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';
import {ProcessModule} from 'app/process/process.module';


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
    FieldNamePipe,
    SortableColumnComponent,
    TasklistComponent,
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
    StoreModule.forRoot({}, {
      metaReducers: [storeLogger]
    }),
    EffectsModule.forRoot([])
  ],
  providers: [
    TaskHelperService,
    ProcessHelperService,
    FilterService
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
