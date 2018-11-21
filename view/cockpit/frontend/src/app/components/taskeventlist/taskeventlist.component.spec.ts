import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import 'rxjs/add/observable/of';
import { TaskEventListComponent } from './taskeventlist.component';
import { Observable } from 'rxjs-compat';
import { FormsModule } from '@angular/forms';

import { TaskEventHelperService } from 'app/services/taskeventhelper.service';
import { FieldNamePipe } from 'app/services/field-name.pipe';
import { NgbCollapse } from '@ng-bootstrap/ng-bootstrap';

describe('Component: TaskEventListComponent', () => {

  let component: TaskEventListComponent;
  let fixture: ComponentFixture<TaskEventListComponent>;

  beforeEach(async(() => {

    TestBed.configureTestingModule({
      imports: [
        FormsModule
      ],
      declarations: [
        FieldNamePipe,
        TaskEventListComponent,
        NgbCollapse
      ],
      providers: [
        { provide: TaskEventHelperService, useValue: { tasks: Observable.of([]) } },
      ],
    }).compileComponents().then(() => {
      // create component and test fixture
      fixture = TestBed.createComponent(TaskEventListComponent);

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
