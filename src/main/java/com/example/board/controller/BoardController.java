package com.example.board.controller;

import com.example.board.entity.Board;
import com.example.board.entity.Comment;
import com.example.board.service.BoardService;
import com.example.board.service.CommentService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final CommentService commentService;

    @GetMapping("/board/list")

    public String list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)Pageable pageable,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        Page<Board> boards;

        if (keyword != null && !keyword.isEmpty()) {
            boards = boardService.search(searchType, keyword, pageable);
        } else {
            boards = boardService.findAll(pageable);
        }

        model.addAttribute("boards", boards);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        return "board/list";
    }

    @GetMapping("/board/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Board board = boardService.findById(id);
        List<Comment> comments = commentService.findByBoardId(id);
        model.addAttribute("board", board);
        model.addAttribute("comments", comments);
        return "board/detail";
    }

    @GetMapping("/board/write")
    public String writeForm() {
        return "board/write";
    }

    @PostMapping("/board/write")
    public String write(Board board) {
        boardService.save(board);
        return "redirect:/board/list";
    }

    @GetMapping("/board/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Board board = boardService.findById(id);
        model.addAttribute("board", board);
        return "board/edit";
    }

    @PostMapping("/board/edit/{id}")
    public String edit(@PathVariable Long id, Board board) {
        boardService.update(id, board);
        return "redirect:/board/detail/" + id;
    }

    @GetMapping("/board/delete/{id}")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/board/list";
    }

}