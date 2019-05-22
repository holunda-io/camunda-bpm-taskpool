import { Component } from '@angular/core';
import { ProcessDefinition } from 'tasklist/models';
import { ProcessHelperService } from 'app/services/process.helper.service';

@Component({
  selector: 'app-process-list',
  templateUrl: './process-list.component.html',
  styleUrls: ['process-list.component.scss']
})
export class ProcesslistComponent {

  processes: Array<ProcessDefinition> = [];

  constructor(
    private processHelper: ProcessHelperService
  ) {
    this.subscribe();
  }

  subscribe() {
    this.processHelper.processes.subscribe((processes: Array<ProcessDefinition>) => {
      this.processes = processes;
    });
  }

}
