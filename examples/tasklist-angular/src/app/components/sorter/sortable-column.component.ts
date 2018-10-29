import {Component, Input, OnInit, HostListener, OnDestroy} from '@angular/core';
import {FilterService, Field, SortDirection} from 'app/services/filter.service';
import { Subscription } from 'rxjs';

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
    this.direction = this.direction === SortDirection.ASC ? SortDirection.DESC : SortDirection.ASC;
    this.filterService.columnSorted({ fieldName: this.fieldName, direction: this.direction });
  }

  constructor(private filterService: FilterService) {

  }

  ngOnInit() {
    // reset the field, if other is used as sorter
    this.columnSortedSubscription = this.filterService.columnSorted$.subscribe( (fieldEvent: Field) => {
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


