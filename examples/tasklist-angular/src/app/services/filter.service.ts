import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

export enum SortDirection {
  ASC = '+',
  DESC = '-'
}

export class Field {
  constructor(public fieldName: string, public direction: SortDirection) {
  }
}


@Injectable()
export class FilterService {

  private columnSortedSource = new BehaviorSubject<Field>({fieldName: 'task.dueDate', direction: SortDirection.DESC });
  private countSubject = new BehaviorSubject<number>(0);

  filter = [''];
  page = 0;
  itemsPerPage = 7;

  columnSorted$ = this.columnSortedSource.asObservable();

  get count() {
    return this.countSubject.asObservable();
  }

  countUpdate(elementCount: number) {
    this.countSubject.next(elementCount);
  }

  columnSorted(event: Field) {
    this.columnSortedSource.next(event);
  }

  getSort(): string {
    const field = this.columnSortedSource.getValue();
    if (field) {
      return field.direction + field.fieldName;
    } else {
      return undefined;
    }
  }
}
