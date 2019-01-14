import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { registerLocaleData } from '@angular/common';
import { FormsModule } from '@angular/forms';
import localeFr from '@angular/common/locales/fr';
import localeDe from '@angular/common/locales/de';
import localeEn from '@angular/common/locales/en';

import { ApiModule, BASE_PATH } from 'process';
import { AppComponent } from 'app/app.component';
import { AppRoutingModule } from 'app/app-routing.module';
import { ApproveTaskComponent } from 'app/tasks/approve-request/approve-task.component';
import { PageNotFoundComponent } from 'app/tasks/page-not-found/page-not-found.component';


registerLocaleData(localeFr, 'fr');
registerLocaleData(localeDe, 'de');
registerLocaleData(localeEn, 'en');


@NgModule({
  declarations: [
    AppComponent,
    PageNotFoundComponent,
    ApproveTaskComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    NgbModule,
    // generated server API
    ApiModule,
    // routing
    AppRoutingModule
  ],
  providers: [
    { provide: BASE_PATH, useValue: '/example-process-approval/rest' }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
