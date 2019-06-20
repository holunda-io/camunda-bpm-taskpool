import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FieldNamePipe} from 'app/shared/field-name.pipe';

@NgModule({
  declarations: [
    FieldNamePipe
  ],
  exports: [FieldNamePipe],
  imports: [
    CommonModule,
  ],
  providers: []
})
export class SharedModule {
}
