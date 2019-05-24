import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {TasklistComponent} from './tasklist.component';
import {NgbPagination, NgbRadioGroup} from '@ng-bootstrap/ng-bootstrap';
import {FormsModule} from '@angular/forms';

import {TaskHelperService} from 'app/services/task.helper.service';
import {SortableColumnComponent} from 'app/task/sorter/sortable-column.component';
import {provideStoreServiceMock} from '@ngxp/store-service/testing';
import {UserStoreService} from 'app/user/state/user.store-service';
import {of} from 'rxjs';
import {FieldNamePipe} from 'app/task/field-name.pipe';
import {TaskStoreService} from 'app/task/state/task.store-service';
import {SortDirection} from 'app/task/state/task.reducer';

describe('Component: TasklistComponent', () => {

  let component: TasklistComponent;
  let fixture: ComponentFixture<TasklistComponent>;

  beforeEach(async(() => {

    TestBed.configureTestingModule({
      imports: [
        FormsModule
      ],
      declarations: [
        TasklistComponent,
        NgbPagination,
        NgbRadioGroup,
        FieldNamePipe,
        SortableColumnComponent
      ],
      providers: [
        provideStoreServiceMock(TaskStoreService, {
          tasks: [],
          sortingColumn$: {fieldName: 'task.dueDate', direction: SortDirection.DESC }
        }),
        provideStoreServiceMock(UserStoreService, {
          currentUserProfile$: {username: '', userIdentifier: '', fullName: ''}
        }),
        {provide: TaskHelperService, useValue: {tasks: of([])}},
      ],
    }).compileComponents().then(() => {
      // create component and test fixture
      fixture = TestBed.createComponent(TasklistComponent);

      // get test component from the fixture
      component = fixture.componentInstance;

      // detect changes
      fixture.detectChanges();
    });
  }));

  it('should create', () => {
    expect(component).toBeDefined();
  });

});
