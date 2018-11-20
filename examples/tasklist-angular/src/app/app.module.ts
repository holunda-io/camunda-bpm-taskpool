import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {registerLocaleData} from '@angular/common';
import {FormsModule} from '@angular/forms';
import localeFr from '@angular/common/locales/fr';
import localeDe from '@angular/common/locales/de';
import localeEn from '@angular/common/locales/en';

import {ApiModule, BASE_PATH} from 'tasklist';
import {AppComponent} from 'app/app.component';
import {TasklistComponent} from 'app/components/tasklist/tasklist.component';
import {TaskHelperService} from 'app/services/task.helper.service';
import {ProfileHelperService} from 'app/services/profile.helper.service';
import {FieldNamePipe} from 'app/services/field-name.pipe';
import {FilterService} from 'app/services/filter.service';
import {SortableColumnComponent} from 'app/components/sorter/sortable-column.component';


registerLocaleData(localeFr, 'fr');
registerLocaleData(localeDe, 'de');
registerLocaleData(localeEn, 'en');


@NgModule({
  declarations: [
    AppComponent,
    FieldNamePipe,
    SortableColumnComponent,
    TasklistComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    NgbModule,
    ApiModule
  ],
  providers: [
    TaskHelperService,
    ProfileHelperService,
    FilterService,
    {provide: BASE_PATH, useValue: '/tasklist/rest'}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
