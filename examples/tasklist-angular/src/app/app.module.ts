import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

import {ApiModule, BASE_PATH} from 'tasklist';
import {AppComponent} from 'app/app.component';
import {TasklistComponent} from 'app/components/tasklist/tasklist.component';
import {TaskHelperService} from 'app/services/task.helper.service';


@NgModule({
  declarations: [
    AppComponent,
    TasklistComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    NgbModule,
    ApiModule
  ],
  providers: [
    TaskHelperService,
    {provide: BASE_PATH, useValue: '/tasklist/rest'}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
