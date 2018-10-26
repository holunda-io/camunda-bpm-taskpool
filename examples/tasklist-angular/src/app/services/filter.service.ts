import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable()
export class FilterService {

  countSubject: BehaviorSubject<number> = new BehaviorSubject<number>(0);

  filter = [''];
  page = 0;
  itemsPerPage = 7;
  sort = [];
}
