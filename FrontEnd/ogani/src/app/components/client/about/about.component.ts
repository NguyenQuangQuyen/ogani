import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css'],
  providers: [MessageService]
})
export class AboutComponent implements OnInit {

  constructor(private messageService: MessageService) { }

  ngOnInit(): void {
  }

}
