import {Component, HostListener, Input, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from 'rxjs';
import {TaskStoreService} from 'app/task/state/task.store-service';
import {Field, SortDirection} from 'app/task/state/task.reducer';

@Component({
  selector: '[sortable-column]',
  templateUrl: './sortable-column.component.html',
  styleUrls: []
})
export class SortableColumnComponent implements OnInit, OnDestroy {

  @Input('sortable-column')
  fieldName: string;

  @Input('sort-direction')
  direction: SortDirection;

  private columnSortedSubscription: Subscription;

  @HostListener('click')
  toggle() {
    const newDirection = this.direction === SortDirection.ASC ? SortDirection.DESC : SortDirection.ASC;
    this.taskStore.updateSortingColumn({ fieldName: this.fieldName, direction: newDirection });
  }

  constructor(private taskStore: TaskStoreService) {
  }

  ngOnInit() {
    // reset the field, if other is used as sorter
    this.columnSortedSubscription =  this.taskStore.sortingColumn$().subscribe( (fieldEvent: Field) => {
      if (fieldEvent && this.fieldName !== fieldEvent.fieldName) {
        this.direction = undefined;
      } else {
        this.direction = fieldEvent.direction;
      }
    });
  }

  ngOnDestroy() {
    this.columnSortedSubscription.unsubscribe();
  }
}


