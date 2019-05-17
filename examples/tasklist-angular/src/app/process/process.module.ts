import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ProcesslistComponent} from "app/process/process-list/process-list.component";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
  declarations: [ProcesslistComponent],
  imports: [
    CommonModule,
    NgbModule
  ],
  exports: [
    ProcesslistComponent
  ]
})
export class ProcessModule { }
