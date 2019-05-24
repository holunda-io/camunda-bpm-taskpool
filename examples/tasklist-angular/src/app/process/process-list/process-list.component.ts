import {Component, OnInit} from '@angular/core';
import {ProcessStoreService} from 'app/process/state/process.store-service';
import {ProcessDefinition} from 'app/process/state/process.reducer';
import {Observable} from 'rxjs';

@Component({
  selector: 'tasks-process-list',
  templateUrl: './process-list.component.html',
  styleUrls: ['process-list.component.scss']
})
export class ProcesslistComponent implements OnInit {

  processes$: Observable<ProcessDefinition[]>;

  constructor(
    private processStore: ProcessStoreService
  ) {
  }

  ngOnInit() {
    this.processes$ = this.processStore.startableProcesses$();
  }
}
