import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { TaskHelperService } from 'app/services/task.helper.service';
import 'rxjs/add/observable/of';
import { TasklistComponent } from './tasklist.component';
import { Observable } from 'rxjs-compat';
import { NgbPagination, NgbTabset, NgbRadioGroup } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { FieldNamePipe } from 'app/services/field-name.pipe';
import { FilterService } from 'app/services/filter.service';

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
        FieldNamePipe
      ],
      providers: [
        FilterService,
        { provide: TaskHelperService, useValue: { tasksSubject: Observable.of([]) } },
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
