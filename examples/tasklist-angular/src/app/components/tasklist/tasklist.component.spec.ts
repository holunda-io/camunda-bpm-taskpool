import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { TaskHelperService } from 'app/services/task.helper.service';
import { Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import { TasklistComponent } from './tasklist.component';

describe('Component: TasklistComponent', () => {

  let component: TasklistComponent;
  let fixture: ComponentFixture<TasklistComponent>;
  let taskHelperServiceSpy: jasmine.SpyObj<TaskHelperService>;

  beforeEach(async(() => {

    taskHelperServiceSpy = jasmine.createSpyObj('taskHelperServiceSpy', {
      'tasks': Observable.of([]),
    });

    TestBed.configureTestingModule({
      imports: [
      ],
      declarations: [
        TasklistComponent,
      ],
      providers: [
        { provide: 'TaskHelperService', useValue: taskHelperServiceSpy },
      ],
    }).compileComponents().then(() => {
      // create component and test fixture
      fixture = TestBed.createComponent(TasklistComponent);

      // get test component from the fixture
      component = fixture.componentInstance;
    });
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
