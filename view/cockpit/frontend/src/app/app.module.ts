import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { registerLocaleData } from '@angular/common';
import { FormsModule } from '@angular/forms';
import localeFr from '@angular/common/locales/fr';
import localeDe from '@angular/common/locales/de';
import localeEn from '@angular/common/locales/en';

import { ApiModule, BASE_PATH } from 'cockpit';
import { AppComponent } from 'app/app.component';
import { TaskEventListComponent } from 'app/components/taskeventlist/taskeventlist.component';
import { TaskEventHelperService } from 'app/services/taskeventhelper.service';
import { TaskEventReactiveService } from 'app/services/taskeventreactive.service';
import { FieldNamePipe } from 'app/services/field-name.pipe';

registerLocaleData(localeFr, 'fr');
registerLocaleData(localeDe, 'de');
registerLocaleData(localeEn, 'en');


@NgModule({
  declarations: [
    AppComponent,
    FieldNamePipe,
    TaskEventListComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    NgbModule,
    ApiModule
  ],
  providers: [
    TaskEventHelperService,
    TaskEventReactiveService,
    { provide: BASE_PATH, useValue: '/taskpool-cockpit/rest' }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
